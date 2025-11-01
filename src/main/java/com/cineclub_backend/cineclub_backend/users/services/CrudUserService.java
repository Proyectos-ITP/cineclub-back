package com.cineclub_backend.cineclub_backend.users.services;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.cineclub_backend.cineclub_backend.users.models.User;
import com.cineclub_backend.cineclub_backend.users.repositories.UserRepository;

@Service
public class CrudUserService {
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public CrudUserService(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> getUsersPaginated(String name, String email, Pageable pageable) {
        try {
            List<AggregationOperation> operations = new ArrayList<>();
            List<Criteria> criteriaList = new ArrayList<>();

            if (name != null && !name.trim().isEmpty()) {
                criteriaList.add(Criteria.where("fullName").regex(name, "i"));
            }
            if (email != null && !email.trim().isEmpty()) {
                criteriaList.add(Criteria.where("email").regex(email, "i"));
            }

            if (!criteriaList.isEmpty()) {
                Criteria combinedCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
                operations.add(Aggregation.match(combinedCriteria));
            }

            FacetOperation facetOperation = Aggregation.facet()
                    .and(Aggregation.count().as("total")).as("metadata")
                    .and(
                            Aggregation.sort(pageable.getSort()),
                            Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                            Aggregation.limit(pageable.getPageSize()))
                    .as("data");

            operations.add(facetOperation);

            Aggregation aggregation = Aggregation.newAggregation(operations);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "users", Document.class);

            Document result = results.getUniqueMappedResult();

            if (result == null) {
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            long total = 0;
            Object metadataObj = result.get("metadata");
            if (metadataObj instanceof List<?> metadataList && !metadataList.isEmpty()) {
                Object firstItem = metadataList.get(0);
                if (firstItem instanceof Document metadataDoc) {
                    total = metadataDoc.getInteger("total", 0);
                }
            }

            List<User> users = new ArrayList<>();
            Object dataObj = result.get("data");
            if (dataObj instanceof List<?> dataList) {
                for (Object item : dataList) {
                    if (item instanceof Document doc) {
                        users.add(mongoTemplate.getConverter().read(User.class, doc));
                    }
                }
            }

            return new PageImpl<>(users, pageable, total);

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener usuarios paginados: " + e.getMessage(), e);
        }
    }
}

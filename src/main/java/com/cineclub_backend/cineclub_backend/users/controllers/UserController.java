package com.cineclub_backend.cineclub_backend.users.controllers;

import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import com.cineclub_backend.cineclub_backend.shared.helpers.SerializeHelper;
import com.cineclub_backend.cineclub_backend.users.dtos.FindUserDto;
import com.cineclub_backend.cineclub_backend.users.dtos.UserDto;
import com.cineclub_backend.cineclub_backend.users.models.User;
import com.cineclub_backend.cineclub_backend.users.services.CrudUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints para gestionar usuarios")
public class UserController {

  private final CrudUserService crudUserService;

  public UserController(CrudUserService crudUserService) {
    this.crudUserService = crudUserService;
  }

  @GetMapping("/{id}")
  @Operation(
    summary = "Obtener usuario por ID",
    description = "Retorna la información de un usuario específico por su ID"
  )
  public User getUserById(@PathVariable String id) {
    return crudUserService.getUserById(id);
  }

  @GetMapping("/email/{email}")
  @Operation(
    summary = "Obtener usuario por email",
    description = "Retorna la información de un usuario específico por su email"
  )
  public User getUserByEmail(@PathVariable String email) {
    return crudUserService.getUserByEmail(email);
  }

  @PostMapping
  @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en el sistema")
  public void createUser(@RequestBody Map<String, Object> payload) {
    Object record = Optional.ofNullable(payload.get("record")).orElse(Map.of());
    User user = SerializeHelper.toObject(record, User.class);
    crudUserService.saveUser(user);
  }

  @PatchMapping("/{id}")
  @Operation(
    summary = "Actualizar usuario",
    description = "Actualiza la información de un usuario existente"
  )
  public User updateUser(@PathVariable String id, @RequestBody User user) {
    return crudUserService.saveUser(user);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema por su ID")
  public void deleteUser(@PathVariable String id) {
    crudUserService.deleteUser(id);
  }

  @GetMapping("/")
  @Operation(
    summary = "Listar todos los usuarios",
    description = "Obtiene la lista completa de usuarios sin paginación"
  )
  public List<User> getAllUsers() {
    return crudUserService.getAllUsers();
  }

  @GetMapping("/paginated")
  @Operation(
    summary = "Listar usuarios paginados",
    description = "Obtiene la lista de usuarios de forma paginada con filtros opcionales"
  )
  public PagedResponseDto<User> getUsersPaginated(@ParameterObject FindUserDto findUserDto) {
    Page<User> page = crudUserService.getUsersPaginated(
      findUserDto.getName(),
      findUserDto.getEmail(),
      findUserDto.toPageable()
    );
    return new PagedResponseDto<>(page);
  }

  @GetMapping("/not-friends")
  @Operation(
    summary = "Ver usuarios que no son mis amigos",
    description = "Obtiene los usuarios paginados que no son mis amigos actualmente"
  )
  public PagedResponseDto<UserDto> getNotFriendsPaginated(
    @AuthenticationPrincipal String userId,
    @ParameterObject FindUserDto findUserDto
  ) {
    Page<UserDto> page = crudUserService.getNotFriendsPaginated(
      userId,
      findUserDto.getName(),
      findUserDto.getEmail(),
      findUserDto.toPageable()
    );
    return new PagedResponseDto<>(page);
  }
}

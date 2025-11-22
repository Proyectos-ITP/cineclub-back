export default {
  extends: ['@commitlint/config-conventional'],
  parserPreset: {
    parserOpts: {
      headerPattern: /^(?::[a-z0-9_]+:|[\u23ea-\u3299\u1f000-\u1f644\u1f648-\u1f64f\u1f680-\u1f6ff])\s+(?<type>[a-z]+)(?:\((?<scope>[a-z0-9-_]+)\))?:\s+(?<subject>.*)$/,
      headerCorrespondence: ['type', 'scope', 'subject'],
    },
  },
};

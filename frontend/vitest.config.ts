export default {
  test: {
    globals: true,
    environment: 'jsdom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      include: ['src/app/**/*.ts'],
      exclude: ['**/*.spec.ts', '**/*.module.ts'],
      all: true,
    },
  },
};

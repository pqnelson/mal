#ifndef ENV_H
#define ENV_H

typedef struct Env Env;

Env* env_new();
void env_free(Env *env);
void env_resize(Env *env, size_t new_capacity);
void env_set(Env *env, LispSymbol *key, LispVal *value);
LispVal *env_get(Env *env, LispSymbol *key);

#endif /* ENV_H */

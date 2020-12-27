#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include "debug.h"
#include "types.h"
#include "env.h"
#include "memory.h"

#define ENV_CAPACITY_FACTOR 0.75

typedef struct Entry {
    LispSymbol *key;
    LispVal *value;
    struct Entry *next;
} Entry;

Entry* entry_new(LispSymbol *key, LispVal *value) {
    Entry* entry = alloc(sizeof(*entry));
    if (NULL == entry) abort();
    ref_inc((LispVal *)key);
    ref_inc((LispVal *)value);
    entry->key = key;
    entry->value = value;
    entry->next = NULL;
    return entry;
}

void entry_free(Entry *entry) {
    if (NULL == entry) return;
    ref_dec((LispVal *)entry->key);
    ref_dec((LispVal *)entry->value);
    free(entry);
}

struct Env {
    Entry **table;
    struct Env *outer;
    size_t capacity;
    size_t size;
};

Env* env_new() {
    Env *env = alloc(sizeof(*env));
    if (NULL == env) abort();
    env->capacity = 16;
    env->table = alloc((env->capacity)*(sizeof(struct Entry*)));
    env->outer = NULL;
    env->size = 0;
    return env;
}

void env_free(Env *env) {
    if (NULL == env) return;

    for (size_t i = 0; env->capacity > i; i++) {
        Entry *row = env->table[i];
        Entry *next = NULL;
        while (row != NULL) {
            next = row->next;
            entry_free(row);
            row = next;
        }
    }
}

static size_t indexFor(hash_t hash, size_t length) {
    return hash & (length-1);
}

static void transfer_rows(Entry *old, Entry **new_table, size_t new_length) {
    Entry *e = old;
    while (e != NULL) {
        Entry *next = e->next;
        size_t index = indexFor(symbol_hashCode(e->key), new_length);
        
        if (NULL == new_table[index]) {
            new_table[index] = e;
            e->next = NULL;
        } else {
            e->next = new_table[index];
        }
        e = next;
    }
}

static void transfer(Entry **old_table, Entry **new_table, size_t length, size_t new_length) {
    for(size_t i = 0; i < length; i++) {
        transfer_rows(old_table[i], new_table, new_length);
    }
}

void env_resize(Env *env, size_t new_capacity) {
    if ((env->capacity) >= new_capacity) return;

    Entry** new_table = alloc(new_capacity * sizeof(struct Entry*));
    if (NULL == new_table) abort();
    
    transfer(env->table, new_table, env->capacity, new_capacity);
    free(env->table);
    env->table = new_table;
    env->capacity = new_capacity;
}

void env_set(Env *env, LispSymbol *key, LispVal *value) {
    assert(NULL != env);
    assert(NULL != key);
    assert(NULL != value);
    hash_t h = symbol_hashCode(key);
    size_t index = indexFor(h, env->capacity);
    Entry *entry = env->table[index];
    printf("env_set() entry==null? %s \n",
           (NULL == entry ? "true" : "false"));
    if (NULL == entry) {
        Entry *new_entry = entry_new(key, value);
        new_entry->next = entry;
        env->table[indexFor(h, env->capacity)] = new_entry;
        env->size++;
        
        if ((env->size) >= (size_t)((env->capacity)*ENV_CAPACITY_FACTOR)) {
            env_resize(env, 2*env->capacity);
        }
    } else {
        Entry *e = entry;
        while (e != NULL) {
            if (symbol_equals(key, e->key)) {
                ref_dec(e->value);
                e->value = value;
                ref_inc(value);
                return;
            }
            e = e->next;
        }
    }
}

LispVal *env_get(Env *env, LispSymbol *key) {
    assert(NULL != env);
    assert(NULL != key);
    hash_t h = symbol_hashCode(key);
    Entry *e = NULL;
    for(e = env->table[indexFor(h, env->capacity)]; e != NULL; e = e->next) {
        if (symbol_equals(key, e->key)) {
            return e->value;
        }
    }
    return NULL;
}

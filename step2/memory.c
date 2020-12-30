#include "memory.h"
#include "debug.h"

/*@ behavior zero_bytes_requested:
  @   assumes size == 0;
  @   allocates \nothing;
  @   ensures \result == \null;
  @ behavior error:
  @   assumes size > 0;
  @   assumes !allocable(size);
  @   allocates \nothing;
  @   ensures \result == \null;
  @ behavior default:
  @   assumes size > 0;
  @   assumes allocable(size);
  @   allocates \result;
  @   ensures \fresh(\result, size);
  @ complete behaviors;
  @ disjoint behaviors;
  @*/
void* alloc(size_t size) {
    /* C standard doesn't specify what to do with malloc(0) */
    if (0 == size) {
        return NULL;
    } else {
        //@ assert size > 0;
        void *block = malloc(size);
        if (NULL == block) {
            eprintf("Tried to allocate a %lu-byte block, but failed.\n",
                    size);
        }
        return block;
    }
}

void* array_alloc(size_t array_size, size_t element_size) {
    if (0 == element_size || 0 == array_size) return NULL;
    void *block = calloc(element_size, array_size);
    if (NULL == block) {
        eprintf("Tried to allocate array of length %lu with element size %lu but failed.\n",
                array_size, element_size);
    }
    return block;
}

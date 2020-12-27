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


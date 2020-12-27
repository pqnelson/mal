#include "memory.h"

// //@ ghost boolean HasErrorWhenAllocating = false;

/*@ requires HasErrorWhenAllocating = false;
  @ behavior zero_bytes_requested:
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
        void *result = malloc(size);
        if (NULL == result) {
            // //@ ghost HasErrorWhenAllocating = true;
        }
        return result;
    }
}


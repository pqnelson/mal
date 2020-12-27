#ifndef MEMORY_H
#define MEMORY_H

#ifndef STDLIB_H
#define STDLIB_H
#include <stdlib.h>
#endif /* STDLIB_H */

/**
 * Allocates requested amount of bytes.
 * 
 * If @c{size} is zero, return the @c{NULL} pointer instead.
 */
void* alloc(size_t size);

#endif /* MEMORY_H */

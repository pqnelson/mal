#ifndef DEBUG_H
#define DEBUG_H

#include <stdio.h>

#define debugging 1

#if debugging
#define debug(...)  printf(__VA_ARGS__)
#define TRACE(...) printf("TRACE: "); printf(__VA_ARGS__)
#define WARN(...) printf("WARN: "); printf(__VA_ARGS__)
#else
#define debug(...) {}
#define TRACE(...) {}
#define WARN(...) {}
#endif /* debugging */

#define eprintf(...) fprintf(stderr, __VA_ARGS__)

#endif

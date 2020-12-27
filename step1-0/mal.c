/**
 * Tests if we can read a string into an S-expression.
 */
#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include "memory.h"
#include "scanner.h"
#include "types.h"
#include "reader.h"
#include "printer.h"

void work_example(char *test_program) {
    size_t length = strlen(test_program)+1;
    char *src = alloc(length+1);
    snprintf(src, length+1, "%s", test_program);
    src[length] = '\0';

    LispVal *value = read_str(src);
    printf("src == NULL ? %s\n", (NULL == src) ? "true" : "false");
    printf("value:\n");
    print_value(stdout, value);
    printf("\n");
    val_free(value);
}

void good_example() {
    work_example("(this\nis (a (test)to) see)");
}

void bad_example() {
    work_example("(this should fail");
}

void example_with_nil() {
    work_example("(foo (bar () baz) spam)");
}

int main() {
    good_example();
    example_with_nil();
    bad_example();
}

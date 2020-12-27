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
    printf("value:\n\t");
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

void comment_example() {
    work_example("(foo ;(bar () baz)\n spam eggs)");
}

void double_check_nil() {
    char test_program[] = "()";
    size_t length = strlen(test_program)+1;
    char *src = alloc(length+1);
    snprintf(src, length+1, "%s", test_program);
    src[length] = '\0';

    LispVal *value = read_str(src);

    printf("value == &nil ? %s\n",
           ((LispVal *)&nil == value) ? "true" : "false");
    
    printf("value:\n\t");
    print_value(stdout, value);
    printf("\n");
    val_free(value);
}

void int_example0() {
    work_example("1234");
}

void int_example() {
    work_example("(= 32 (+ x (sq y)))");
}

void float_example() {
    work_example("(= area (* 3.141 (sq r)))");
}

void pi_example() {
    work_example("3.14159");
}

int main() {
    good_example();
    example_with_nil();
    double_check_nil();
    comment_example();
    
    int_example0();
    int_example();
    pi_example();
    float_example();

    // bad_example();

    return 0;
}

/**
 * Scan S-expressions.
 * 
 * This experiments with implementing a scanner for S-expressions,
 * producing a stream of tokens on demand.
 */
#include <assert.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include "memory.h"
#include "scanner.h"

int main() {
    struct token *lexeme = NULL;
    char input[] = "this\nis (a (test)to) see";
    int length = strlen(input);

    char *src = malloc(length+1);
    snprintf(src, length+1, "%s", input);
    src[length] = '\0';

    Scanner *scanner = scanner_new(src);

    /* debugging */
    printf("SRC length = %ld\n\n", strlen(src));
    printf("INPUT length = %d\n\n", length);
    printf("src[24] == '\\0'? %s\n\n", (src[length] == '\0' ? "true" : "false"));

    /* tokenize the input */
    int max_iter = length;
    for (int i = 0; (i < max_iter) && scanner_has_next(scanner); i++) {
        assert (NULL == lexeme);
        lexeme = scanner_next(scanner);

        printf("LINE %ld, OFFSET %ld, TOKEN ='",
               scanner->line, scanner->offset);
        token_print(stdout, lexeme);
        printf("'\n");

        if (NULL != lexeme) {
            printf("Freeing token...\n");
            token_free(lexeme);
            lexeme = NULL;
        }
        assert(NULL == lexeme);
    }
    assert(NULL == lexeme);
    printf("Finally freeing scanner\n");
    scanner_free(scanner);
    return 0;
}



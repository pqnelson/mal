/**
 * XUnit Testing Framework.
 *
 * A bare-bones single file for creating TestCases and TestSuites. To construct
 * a new test case, simply write `test_case("my test case", function () {...})`.
 * You need to stick it into a TestSuite, and register the suite, before it
 * will be detected by the TestRunner.
 *
 * To run the unit tests, simply type in `run_tests()` into a console. Failures
 * and tests with unexpected exceptions will be printed to the console, along
 * with cumulative summaries for the test suites (and a grand total of all the
 * tests, failures, etc., and time it took in milliseconds).
 *
 * @example
 * register_suite(new TestSuite("Reader Tests", [
 *  test_case("- is a symbol, not a float", function () {
 *    var result = read_str("-");
 *    return (symbol_QMARK_(result));
 *  }),
 *  // and more tests
 * ]));
 */

/**
 * Construct a new test case.
 *
 * @param {string} name - The name of the test, to be printed upon failure.
 * @param {Function} fn - The body of the test as a function object,
 *                        with zero arguments. Returns boolean, true represents
 *                        success.
 * @constructor
 */
function TestCase(name, fn) {
  this.name = name;
  this.fn = fn;
}
function is_TestCase(obj) { return (obj instanceof TestCase); }

/**
 * Get the name of the TestCase.
 */
TestCase.prototype.name = function() { return this.name; };

/**
 * Run a test case.
 *
 * @returns True iff test case succeeded.
 */
TestCase.prototype.run = function() { return this.fn(); };

/**
 * Factory for new test cases, for brevity.
 *
 * @param {string} name - The name of the test case for logging failures.
 * @param {Function} fn - The test which will be executed when run.
 * @returns {TestCase} A new test case.
 */
function test_case(name, fn) {
  return new TestCase(name, fn);
}

/**
 * Composite pattern applied to TestCase objects.
 *
 * @param {string} name - The name of the test suite
 * @param {(TestSuite|TestCase)[]=} initial_tests - An optional array of initial
 * test cases or suites.
 */
function TestSuite(name, initial_tests) {
  this.name = name;
  this.tests = initial_tests || [];
}
function is_TestSuite(obj) { return (obj instanceof TestSuite); }

TestSuite.prototype.name = function() { return this.name; };

/**
 * Register a TestSuite or TestCase.
 *
 * @param {(TestSuite|TestCase)} test - A new test to add to the suite.
 */
TestSuite.prototype.add_tests = function(test) {
  if ((!is_TestCase(test)) && (!is_TestSuite(test))) {
    throw Error("Trying to add something that is neither a TestCase nor a TestSuite to "+(this.name));
  }
  this.tests.push(test);
};
/**
 * Do we print the successes, too? If so, toggle this variable to true.
 */
var verbose_testing = false;

/**
 * Run every test in the suite.
 *
 * Prints failures and errors, and the number of milliseconds it took.
 *
 * @param {boolean=} print_results - Toggle if we want to print the summary
 * to the console.
 *
 * @returns {total: number, fail: number, success: number, errors: number, time: number} JSON object of test results.
 */
TestSuite.prototype.run = function(print_results) {
  print_results = print_results || !!print_results;
  var total = 0, fail = 0, success = 0, errors = 0;
  var start = new Date();
  for (const test of this.tests) {
    if (is_TestSuite(test)) {
      var results = test.run(print_results);
      total += results.total;
      fail += results.fail;
      success += results.success;
      errors += results.errors;
    } else {
      total++;
      try {
        if (test.run()) {
          if (verbose_testing) {
            console.log(test.name, "- success");
          }
          success++;
        } else {
          console.log(test.name, "- fail");
          fail++;
        }
      } catch (error) {
        console.log(test.name, "- error ");
        errors++;
      }
    }
  }
  var end = new Date();
  var timeDiffInMs = end - start;
  if (print_results) {
    console.log(this.name, "results took",
                timeDiffInMs, "ms, total: ", total,
                ", success: ", success,
                ", failures: ", fail,
                ", errors: ", errors);
  }
  return {success: success,
          total: total,
          fail: fail,
          errors: errors,
          timeDiff: timeDiffInMs};
};

// "Private" variable - tracks all the tests we want to register and run.
const __tests = new TestSuite("tests");

/**
 * Register a TestSuite with the test runner.
 *
 * @param {TestSuite} suite - Test suite to be added to the schedule.
 * @returns Nothing.
 */
function register_suite(suite) {
  __tests.add_tests(suite);
}

/**
 * Executes tests and prints summaries to console.
 *
 * @returns Nothing.
 */
function run_tests() {
  var results = __tests.run(true);
}

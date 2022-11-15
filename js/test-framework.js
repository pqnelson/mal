/**
 * A simple BDD test function.
 *
 * @param {string} desc - The property we are testing.
 * @param fn - A closure for the function which throws an error upon failure.
 * @see {@link https://brightsec.com/blog/unit-testing-javascript/}
 */
(function(){
  'use strict';

   function it(desc, fn) {
    try {
      fn();
      console.log(desc);
    } catch (error) {
      console.log('\n');
      console.log(desc);
      console.error(error);
    }
  }
})();

function TestCase(name, fn) {
  this.name = name;
  this.fn = fn;
}
function is_TestCase(obj) { return (obj instanceof TestCase); }

TestCase.prototype.name = function() { return this.name; };
TestCase.prototype.run = function() { return this.fn(); };

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
 */
TestSuite.prototype.run = function() {
  var total = 0, fail = 0, success = 0, errors = 0;
  var start = new Date();
  for (const test of this.tests) {
    if (is_TestSuite(test)) {
      test.run();
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
  console.log(this.name, " results took", timeDiffInMs, "ms, total: ", total, ", success: ", success, ", failures: ", fail, ", errors: ", errors);
};

// "Private" variable - tracks all the tests we want to register and run.
const __tests = new TestSuite("tests");

function register_suite(suite) {
  __tests.add_tests(suite);
}

function run_tests() {
  __tests.run();
}

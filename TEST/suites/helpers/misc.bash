# Prints a descriptive error message to stderr.
# And terminates the calling process immediately
# by returning a general error to its parent process.
echo_err() {

  printf "$1\n" >&2
  exit 1
}

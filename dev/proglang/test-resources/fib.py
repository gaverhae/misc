def fib(n):
  if n == 0:
    return 1
  else:
    if n == 1:
      return 1
    else:
      return fib(n + -2) + fib(n + -1)

print(fib(10))

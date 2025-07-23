b = 5

def side_effect_y(a):
  return a + b

b = 7

print(side_effect_y(5))

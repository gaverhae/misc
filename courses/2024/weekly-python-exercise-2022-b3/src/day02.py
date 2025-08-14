def tr(in_set, out_set):
    if len(in_set) == 0 or len(out_set) == 0:
        raise TypeError
    d = { key : out_set[min(idx, len(out_set) - 1)] for (idx, key) in enumerate(in_set) }
    return lambda s: ''.join(d.get(c, c) for c in s)

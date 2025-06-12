# gity

For the longest time, I've been doing most of my git visualization with a
wonderful utility called [GitX]. However, that utility has two main problems:

- It only exists for macOS, and I am sometimes working on other platforms, or
  with people who use other platforms.
- It has been unmaintained for over 15 years at this point.

There are a couple forks floating aound the web, but they're a bit hard to
track. I have been using the same binary, which I've transplanted from machine
to machine, for over ten years. It's obviously still an Intel application.

I've been wanting to write my own for a fairly long time now, but it's never
quite been a priority as the existing binary I have still worked. Over the last
week, though, in this very project, the application crashes every time it tries
to render the git history. I'm not sre what's happening or what has changed (it
has worked flawlessly on this repo since this repo was created); it could be a
bug in the app, it could be a bug in Rosetta2.

I've also been looking for an excuse to try [Humble UI], as well as [native
compilation of Clojure programs][humble-graal] through [GraalVM].

So here goes nothing, as they say. We'll see how far I get.

[Humble UI]: https://github.com/HumbleUI/HumbleUI
[GraalVM]: https://www.graalvm.org
[GitX]: https://github.com/pieter/gitx
[humble-graal]: https://github.com/dundalek/humble-graal

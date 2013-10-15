#include <cstdio>
#include <cassert>

class Solution {
public:
  typedef long long LL;
  int divide(int dividend, int divisor) {
    bool clear;
    LL a = dividend;
    LL b = divisor;
    bool minus = false;
    if(a < 0) {
      minus = !minus;
      a = -a;
    }
    if(b < 0) {
      minus = !minus;
      b = -b;
    }
    LL c = divide(a, b, &clear);
    if(minus) {
      c = -c;
    }
    return c;
  }

  LL divide(LL dividend, LL divisor, bool* clear) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    int x = msb(divisor);
    LL res = 0;
    for(int i = (62 - x); i >= 0; i--) {
      LL base = 1;
      LL saved = divisor;
      for(int j = 0; j < i; j++) {
        saved <<= 1;
        base <<= 1;
      }
      if(dividend >= saved) {
        dividend -= saved;
        res += base;
      }
    }
    *clear = false;
    if(dividend == 0) {
      *clear = true;
    }
    return res;
  }

  int msb(LL divisior) {
    int n = 0;
    for(int i = 0; i < 63; i++) {
      if(divisior & 0x1) {
        n = i;
      }
      divisior >>= 1;
    }
    return n;
  }
};

int main() {
  Solution s;
  assert(s.divide(5, 4) == 1);
  assert(s.divide(5, 6) == 0);
  assert(s.divide(6, 2) == 3);
  assert(s.divide(-1, 1) == -1);
  assert(s.divide(-1, 2) == 0);
  assert(s.divide(5, -2) == -2);
}

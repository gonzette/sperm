#include <cstdio>
#include <string>
using namespace std;

class Solution {
public:
  int reverse(int x) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    bool flip = false;
    if(x < 0) {
      flip = true;
      x = -x;
    }
    string s;
    while(x) {
      s += (x % 10) + '0';
      x /= 10;
    }
    int v = 0;
    for(int i = 0; i < s.size(); i++) {
      v = v * 10 + (s[i] - '0');
    }
    if(flip) {
      v = -v;
    }
    return v;
  }
};

int main() {
  Solution s;
  printf("%d\n", s.reverse(-123));
  printf("%d\n", s.reverse(123));
  return 0;
}

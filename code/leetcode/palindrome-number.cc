#include <cstdio>
#include <climits>
using namespace std;

class Solution {
public:
  bool isPalindrome(int x) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(x < 0) {
      return false;
    }
    int v = 0;
    int saved = x;
    while(x) {
      int b = x % 10;
      if(v > (INT_MAX - b) / 10) {
        return false;
      }
      v = v * 10 + b;
      x /= 10;
    }
    return v == saved;
  }
};

int main() {
  Solution s;
  printf("%d\n", s.isPalindrome(-1));
  printf("%d\n", s.isPalindrome(12321));
  return 0;
}

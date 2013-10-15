#include <cstdio>
#include <cstring>

class Solution {
public:
  int singleNumber(int A[], int n) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    int mask[32]; // sizeof(int) == 32;
    memset(mask, 0, sizeof(mask));
    for(int i = 0; i < n; i++) {
      R(A[i], mask);
    }
    int code = S(mask);
    return code;
  }
  void R(int a, int mask[]) {
    for(int i = 0; i < 32; i++) {
      if(a & 0x1) {
        mask[i] = (mask[i] + 1) % 3;
      }
      a >>= 1;
    }
  }
  int S(int mask[]) {
    int code = 0;
    for(int i = 31; i >= 0; i--) {
      code = (code << 1) + mask[i];
    }
    return code;
  }
};

int main() {
  return 0;
}

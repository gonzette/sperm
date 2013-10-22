#include <cstdio>

class Solution {
public:
  void merge(int A[], int m, int B[], int n) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    int p = m + n - 1;
    int x = m - 1;
    int y = n - 1;
    while(x >= 0 && y >= 0) {
      if(A[x] >= B[y]) {
        A[p] = A[x];
        x--;
      } else {
        A[p] = B[y];
        y--;
      }
      p--;
    }
    while(y >= 0) {
      A[p--] = B[y--];
    }
  }
};

int main() {
  return 0;
}

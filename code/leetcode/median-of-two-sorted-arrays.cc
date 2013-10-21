#include <cstdio>

class Solution {
public:
  double findMedianSortedArrays(int A[], int m, int B[], int n) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(m == 0 && n == 0) {
      return 0;
    }
    int mn = m + n;
    if(mn & 0x1) {
      return R(A, 0, m - 1, B, 0, n - 1, mn >> 1);
    } else {
      int x = R(A, 0, m - 1, B, 0, n - 1, (mn >> 1) - 1);
      int y = R(A, 0, m - 1, B, 0, n - 1, (mn >> 1));
      return (x + y) * 0.5;
    }
  }
};

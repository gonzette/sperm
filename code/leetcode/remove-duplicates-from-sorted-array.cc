#include <cstdio>
class Solution {
public:
  int removeDuplicates(int A[], int n) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(n == 0) return 0;
    int c = 0;
    for(int i = 1; i < n; i++) {
      if(A[i] == A[c]) continue;
      else A[++c] = A[i];
    }
    return c + 1;
  }
};
int main() {
  return 0;
}

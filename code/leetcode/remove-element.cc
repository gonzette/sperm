/* coding:utf-8
 * Copyright (C) dirlt
 */

class Solution {
public:
  int removeElement(int A[], int n, int elem) {
    // IMPORTANT: Please reset any member data you declared, as
    // the same Solution instance will be reused for each test case.
    int x = 0;
    for(int i = 0; i < n; i++) {
      if(A[i] == elem) continue;
      A[x++] = A[i];
    }
    return x;
  }
};

int main() {
  return 0;
}

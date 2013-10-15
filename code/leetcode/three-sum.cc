#include <vector>
#include <algorithm>
#include <cstdio>
using namespace std;

class Solution {
public:
  vector<vector<int> > threeSum(vector<int>& num) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    vector< vector<int> > res;
    sort(num.begin(), num.end());
    int n = num.size();
    for(int i = 0; i < (n - 2); i++) {
      int xi = num[i];
      if(i != 0 && num[i] == num[i - 1]) continue;
      for(int j = n - 1; j >= (i + 2); j--) {
        int xj = num[j];
        if(j != (n - 1) && num[j] == num[j + 1]) continue;
        if(xj < xi) break;
        int xz = -(xi + xj);
        if(xz > xj) break;
        if(xz < xi) continue;
        if(isOK(num, i + 1, j - 1, xz)) {
          vector<int> r;
          r.push_back(xi);
          r.push_back(xz);
          r.push_back(xj);
          res.push_back(r);
        }
      }
    }
    P(res);
    return res;
  }

  void P(const vector< vector<int> >& s) {
    for(int i = 0; i < s.size(); i++) {
      printf("[");
      for(int j = 0; j < s[i].size(); j++) {
        printf("%d ", s[i][j]);
      }
      printf("]\n");
    }
  }

  bool isOK(const vector<int>& num, int s, int e, int x) {
    int u = 0;
    int v = (e - s);
    while(u <= v) {
      int m = (u + v) / 2;
      if(num[m + s] == x) return true;
      if(num[m + s] > x) {
        v = m - 1;
      } else {
        u = m + 1;
      }
    }
    return false;
  }
};


int main() {
  Solution s;
  {
    int A[] = { -1, 0, 1, 2, -1, -4};
    vector<int> N;
    for(int i = 0; i < 6; i++) {
      N.push_back(A[i]);
    }
    s.threeSum(N);
  }
  return 0;
}

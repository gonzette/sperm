#include <vector>
#include <algorithm>
#include <cstdio>
using namespace std;

class Solution {
public:
  int threeSumClosest(vector<int>& number, int target) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    sort(number.begin(), number.end());
    int n = number.size();
    int res = -1;
    bool init = false;
    for(int i = 0; i < (n - 2); i++) {
      int xi = number[i];
      for(int j = n - 1; j >= (i + 2); j--) {
        int xj = number[j];
        int xz = target - xi - xj;
        int c = C(number, i + 1, j - 1, xz); // c is relative offset to xz.
        if(!init || abs(res) > abs(c)) {
          res = c;
          init = true;
        }
        if(c == 0) {
          break;
        }
      }
    }
    return res + target;
  }
  int C(const vector<int>& number, int s, int e, int x) {
    int xp = B(number, s, e, x);
    //x in between (xp,xp+1)
    bool init = false;
    int res = -1;
    if(xp >= s && (!init || abs(res) > abs(number[xp] - x))) {
      res = number[xp] - x;
      init = true;
    }
    if((xp + 1) <= e && (!init || abs(res) > abs(number[xp + 1] - x))) {
      res = number[xp + 1] - x;
      init = true;
    }
    return res;
  }
  int B(const vector<int>& number, int s, int e, int x) {
    int u = 0;
    int v = (e - s);
    while(u <= v) {
      int m = (u + v) / 2;
      if(number[m + s] == x) return m + s;
      if(number[m + s] > x) {
        v = m - 1;
      } else {
        u = m + 1;
      }
    }
    return (u - 1 + s);
  }
};


int main() {
  Solution s;
  {
    int A[] = {0, 1, 2};
    vector<int> N;
    for(int i = 0; i < 3; i++) {
      N.push_back(A[i]);
    }
    printf("%d\n", s.threeSumClosest(N, 4));
  }
  return 0;
}

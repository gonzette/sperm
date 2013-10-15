#include <algorithm>
#include <cmath>
#include <vector>
#include <cstdio>
using namespace std;

class Solution {
public:
  int n;
  int maxArea(vector<int>& height) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    n = height.size();
    if(n == 0) {
      return 0;
    }
    int res = 0;
    int lm = 0;
    for(int i = 0; i < n; i++) {
      if(height[i] <= lm) continue; // prune.
      lm = height[i];
      for(int j = n - 1; j > i; j--) {
        res = max(res, (j - i) * min(height[i], height[j]));
        if(height[j] >= height[i]) {
          break;
        }
      }
    }
    return res;
  }
};

int main() {
  Solution s;
  {
    vector<int> x;
    x.push_back(1);
    x.push_back(1);
    printf("%d\n", s.maxArea(x));
  }
  {
    vector<int> x;
    x.push_back(1);
    x.push_back(2);
    x.push_back(1);
    printf("%d\n", s.maxArea(x));
  }
  {
    vector<int> x;
    x.push_back(1);
    x.push_back(2);
    x.push_back(3);
    printf("%d\n", s.maxArea(x));
  }
  {
    vector<int> x;
    x.push_back(3);
    x.push_back(2);
    x.push_back(1);
    printf("%d\n", s.maxArea(x));
  }
  {
    vector<int> x;
    x.push_back(3);
    x.push_back(2);
    x.push_back(3);
    printf("%d\n", s.maxArea(x));
  }
  return 0;
}

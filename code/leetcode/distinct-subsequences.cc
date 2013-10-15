#include <string>
#include <cstdio>
#include <algorithm>
using namespace std;

class Solution {
public:
  int numDistinct(string s, string t) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    const int n = s.size();
    const int m = t.size();
    if(m > n) {
      return 0;
    }
    int dp[2][m];
    memset(dp, 0, sizeof(dp));
    int id = 0;
    for(int i = 0; i < n; i++) {
      for(int j = 0; j <= (i, m); j++) {
        dp[1 - id][j] = 0;
        if(i != j) {
          dp[1 - id][j] += dp[id][j];
        }
        if(s[i] == t[j]) {
          if(j == 0) {
            dp[1 - id][j] += 1;
          } else {
            dp[1 - id][j] += dp[id][j - 1];
          }
        }
        //printf("i = %d, j = %d, v = %d\n",i,j,dp[1-id][j]);
      }
      id = 1 - id;
    }
    return dp[id][m - 1];
  }
};

int main() {
  Solution s;
  printf("%d\n", s.numDistinct("cccc", "c"));
  return 0;
}


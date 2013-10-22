#include <cstdio>
#include <cstring>

class Solution {
public:
  int numTrees(int n) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    const int N = n;
    int dp[N + 1];
    memset(dp, 0, sizeof(dp));
    dp[0] = 1;
    dp[1] = 1;
    for(int i = 2; i <= N; i++) {
      for(int j = 1; j <= i; j++) {
        dp[i] += (dp[j - 1] * dp[i - j]);
      }
    }
    return dp[N];
  }
};

int main() {
  Solution s;
  printf("%d\n", s.numTrees(3));
  return 0;
}

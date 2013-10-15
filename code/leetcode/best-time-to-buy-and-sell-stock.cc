#include <cstdio>
#include <algorithm>
#include <vector>

using namespace std;

class Solution {
public:
  int maxProfit(vector<int>& prices) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    const int n = prices.size();
    if(n == 0) {
      return 0;
    }
    int dp[n];
    // dp[0][i] (0,i-1) max.
    P0(prices, dp);
    return dp[n - 1];
  }

  void P0(vector<int>& prices, int dp[]) {
    int f = 0;
    int t = prices.size() - 1;

    dp[f] = 0;
    int s = prices[f];
    for(int i = f + 1; i <= t; i++) {
      dp[i] = max(dp[i - 1], prices[i] - s);
      s = min(s, prices[i]);
    }
  }
};

int main() {
  return 0;
}

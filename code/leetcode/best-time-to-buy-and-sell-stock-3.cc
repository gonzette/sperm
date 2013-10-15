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
    int dp[2][n];
    // dp[0][i] (0,i-1) max.
    // dp[1][i] (i,n-1) max.
    P0(prices, dp[0]);
    P1(prices, dp[1]);

    int result = 0;
    for(int i = 0; i < n; i++) {
      int v1 = 0;
      if(i != 0) {
        v1 = dp[0][i - 1];
      }
      int v2 = dp[1][i];
      //printf("v1 = %d, v2 = %d, i = %d\n",v1,v2,i);
      result = max(v1 + v2, result);
    }

    return result;
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

  void P1(vector<int>& prices, int dp[]) {
    int f = 0;
    int t = prices.size() - 1;

    dp[t] = 0;
    int s = prices[t];
    for(int i = t - 1; i >= f; i--) {
      dp[i] = max(dp[i + 1], s - prices[i]);
      s = max(s, prices[i]);
    }
  }
};

int main() {
  Solution s;
  int a[] = {1, 2, 3, 4, 5, 6};
  vector<int> prices;
  for(int i = 0; i < 6; i++) {
    prices.push_back(a[i]);
  }
  printf("%d\n", s.maxProfit(prices));
  return 0;
}

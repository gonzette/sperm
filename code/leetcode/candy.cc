#include <vector>
#include <algorithm>

using namespace std;

class Solution {
public:
  int candy(vector<int>& ratings) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    const int N = ratings.size();
    if(N == 0) {
      return 0;
    }
    int dp[2][N];
    L(ratings, dp[0]);
    R(ratings, dp[1]);
    int sum = 0;
    for(int i = 0; i < N; i++) {
      sum += max(dp[0][i], dp[1][i]);
    }
    return sum;
  }

  void L(const vector<int>& ratings, int dp[]) {
    int n = ratings.size();
    dp[0] = 1;
    for(int i = 1; i < n; i++) {
      if(ratings[i] > ratings[i - 1]) {
        dp[i] = dp[i - 1] + 1;
      } else {
        dp[i] = 1;
      }
    }
  }

  void R(const vector<int>& ratings, int dp[]) {
    int n = ratings.size();
    dp[n - 1] = 1;
    for(int i = n - 2; i >= 0; i--) {
      if(ratings[i] > ratings[i + 1]) {
        dp[i] = dp[i + 1] + 1;
      } else {
        dp[i] = 1;
      }
    }
  }
};

int main() {
  return 0;
}

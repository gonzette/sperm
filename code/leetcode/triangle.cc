#include <vector>
#include <algorithm>

using namespace std;

class Solution {
public:
  int minimumTotal(vector<vector<int> >& triangle) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    const int n = triangle.size();
    if(n == 0) {
      return 0;
    }
    int dp[2][n];
    int id = 0;
    dp[id][0] = triangle[0][0];
    for(int i = 1; i < n; i++) {
      for(int j = 0; j <= i; j++) {
        if(j == 0) {
          dp[1 - id][j] = dp[id][j];
        } else if(j == i) {
          dp[1 - id][j] = dp[id][j - 1];
        } else {
          dp[1 - id][j] = min(dp[id][j], dp[id][j - 1]);
        }
        dp[1 - id][j] += triangle[i][j];
      }
      id = 1 - id;
    }
    int result = dp[id][0];
    for(int i = 1; i < n; i++) {
      result = min(result, dp[id][i]);
    }
    return result;
  }
};

int main() {
  return 0;
}

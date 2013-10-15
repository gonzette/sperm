#include <vector>
using namespace std;

class Solution {
public:
  vector<int> getRow(int rowIndex) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    const int n = rowIndex;
    vector<int> res;
    if(n == 0) {
      res.push_back(1);
      return res;
    }
    int dp[2][n];
    int id = 0;
    dp[0][0] = 1;
    for(int i = 1; i <= n; i++) {
      for(int j = 0; j <= i; j++) {
        if(j == 0) {
          dp[1 - id][j] = dp[id][j];
        } else if(j == i) {
          dp[1 - id][j] = dp[id][j - 1];
        } else {
          dp[1 - id][j] = dp[id][j] + dp[id][j - 1];
        }
      }
      id = 1 - id;
    }
    for(int i = 0; i <= n; i++) {
      res.push_back(dp[id][i]);
    }
    return res;
  }
};

int main() {
  return 0;
}

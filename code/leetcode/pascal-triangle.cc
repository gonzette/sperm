#include <vector>
using namespace std;

class Solution {
public:
  vector< vector<int> > generate(int numRows) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    const int n = numRows;
    vector< vector<int> > res;
    if(n == 0) {
      return res;
    }
    int dp[2][n];
    int id = 0;
    dp[0][0] = 1;

    vector<int> vi;
    vi.push_back(1);
    res.push_back(vi);

    for(int i = 1; i < n; i++) {
      vector<int> vi;
      for(int j = 0; j <= i; j++) {
        if(j == 0) {
          dp[1 - id][j] = dp[id][j];
        } else if(j == i) {
          dp[1 - id][j] = dp[id][j - 1];
        } else {
          dp[1 - id][j] = dp[id][j] + dp[id][j - 1];
        }
        vi.push_back(dp[1 - id][j]);
      }
      res.push_back(vi);
      id = 1 - id;
    }
    return res;
  }
};

int main() {
  return 0;
}

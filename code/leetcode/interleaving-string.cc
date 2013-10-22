#include <cstdio>
#include <string>
using namespace std;

class Solution {
public:
  bool isInterleave(string s1, string s2, string s3) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    const int n = s1.size();
    const int m = s2.size();
    if((n + m) != s3.size()) return false;
    bool dp[n + 1][m + 1];
    memset(dp, 0, sizeof(dp));
    dp[0][0] = true;
    for(int i = 1; i <= n; i++) {
      dp[i][0] = s1.substr(0, i) == s3.substr(0, i);
    }
    for(int i = 1; i <= m; i++) {
      dp[0][i] = s2.substr(0, i) == s3.substr(0, i);
    }
    for(int i = 1; i <= n; i++) {
      for(int j = 1; j <= m; j++) {
        if((s1[i - 1] == s3[i + j - 1] && dp[i - 1][j]) ||
            (s2[j - 1] == s3[i + j - 1] && dp[i][j - 1])) {
          dp[i][j] = true;
        }
      }
    }
    return dp[n][m];
  }
};

int main() {
  Solution s;
  printf("%d\n", s.isInterleave("aabcc", "dbbca", "aadbbcbcac"));
  printf("%d\n", s.isInterleave("aabcc", "dbbca", "aadbbbaccc"));
  return 0;
}

#include <cstdio>
#include <string>
using namespace std;

class Solution {
public:
  int numDecodings(string s) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    const int n = s.size();
    if(n == 0) return 0;
    int dp[n];
    for(int i = 0; i < n; i++) {
      dp[i] = 0;
      // consider i and i-1.
      if(s[i] != '0') dp[i] += (i > 0 ? dp[i - 1] : 1);
      if(i > 0 && s[i - 1] != '0') {
        int val = (s[i - 1] - '0') * 10 + s[i] - '0';
        if(val <= 26) dp[i] += (i > 1 ? dp[i - 2] : 1);
      }
    }
    return dp[n - 1];
  }
};

int main() {
  Solution s;
  printf("%d\n", s.numDecodings("12"));
  return 0;
}

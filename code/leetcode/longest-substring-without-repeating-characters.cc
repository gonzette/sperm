#include <string>
#include <cstdio>
using namespace std;

class Solution {
public:
  int lengthOfLongestSubstring(string s) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    int mask[256];
    memset(mask, 0, sizeof(mask));
    int t = 0;
    int res = 0;
    for(int i = 0; i < s.size(); i++) {
      char ch = s[i];
      // sink ch.
      mask[ch]++;
      if(mask[ch] > 1) {
        for(;;) {
          char es = s[t++];
          mask[es]--;
          if(mask[ch] == 1) {
            break;
          }
        }
      }
      res = max(res, i - t + 1);
    }
    return res;
  }
};

int main() {
  Solution s;
  printf("%d\n", s.lengthOfLongestSubstring("abcabcdbb"));
  return 0;
}

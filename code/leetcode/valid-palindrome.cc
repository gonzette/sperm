#include <cctype>
#include <string>
#include <vector>
#include <cstdio>
using namespace std;

class Solution {
public:
  bool isPalindrome(string s) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    vector<char> vc;
    for(int i = 0; i < s.size(); i++) {
      if(isalnum(s[i])) {
        vc.push_back(tolower(s[i]));
      }
    }
    int f = 0;
    int t = vc.size() - 1;
    while(f < t) {
      if(vc[f] != vc[t]) {
        return false;
      }
      f++;
      t--;
    }
    return true;
  }
};

int main() {
  Solution s;
  printf("%d\n", s.isPalindrome("A man, a plan, a canal: Panama"));
  return 0;
}

#include <vector>
#include <string>
using namespace std;

class Solution {
public:
  vector<string> letterCombinations(string digits) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    static const char* dict[] = {
      "",
      "",
      "abc",
      "def",
      "ghi",
      "jkl",
      "mno",
      "pqrs",
      "tuv",
      "wxyz",
    };
    vector<string> rs;
    string s;
    R(dict, digits, s, rs);
    return rs;
  }
  void R(const char* dict[], const string& ds, string& s, vector<string>& rs) {
    if(s.size() == ds.size()) {
      rs.push_back(s);
    } else {
      int d = ds[s.size()] - '0';
      for(int i = 0; dict[d][i]; i++) {
        char ch = dict[d][i];
        s += ch;
        R(dict, ds, s, rs);
        //s.pop_back(); // C++0x.
        s = s.substr(0, s.size() - 1);
      }
    }
  }
};

int main() {
  return 0;
}

#include <cstdio>
#include <string>
using namespace std;

// actually there is a O(N^2) with constant space solution.
#if 0

static char dp[1000 * 1000];
class Solution {
public:
  int n;
  string longestPalindrome(string s) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    n = s.size();
    memset(dp, 0xff, sizeof(dp));
    for(int ln = n ; ln >= 1; ln --) {
      for(int i = 0; (i + ln - 1) < n; i++) {
        if(isOK(s, i, i + ln - 1)) {
          string ret = s.substr(i, ln);
          return ret;
        }
      }
    }
  }

  int getIndex(int i, int j) {
    return i * n + j;
  }

  bool isOK(const string& s, int f, int e) {
    if(f >= e) return true;
    char& ch = dp[getIndex(f, e)];
    if(ch == -1) {
      bool ok = (s[f] == s[e]) && isOK(s, f + 1, e - 1);
      ch = ok ? 1 : 0;
      return ok;
    } else {
      return ch == 1;
    }
  }
};

#else

class Solution {
public :
  string expand(const string& s, int f, int e) {
    while(f >= 0 && e < s.size()) {
      if(s[f] == s[e]) {
        f--;
        e++;
      } else {
        break;
      }
    }
    f++;
    e--;
    if(f > e) {
      e = f;
    }
    return s.substr(f, e - f + 1);
  }

  string longestPalindrome(string s) {
    string ln;
    for(int i = 0; i < s.size(); i++) {
      string x = expand(s, i, i);
      if(x.size() > ln.size()) {
        ln = x;
      }
    }
    for(int i = 0; i < s.size() - 1; i++) {
      string x = expand(s, i, i + 1);
      if(x.size() > ln.size()) {
        ln = x;
      }
    }
    return ln;
  }
};

#endif


int main() {
  Solution s;
  printf("%s\n", s.longestPalindrome("iopsajhffgvrnyitusobwcxgwlwniqchfnssqttdrnqqcsrigjsxkzcmuoiyxzerakhmexuyeuhjfobrmkoqdljrlojjjysfdslyvckxhuleagmxnzvikfitmkfhevfesnwltekstsueefbrddxrmxokpaxsenwlgytdaexgfwtneurhxvjvpsliepgvspdchmhggybwupiqaqlhjjrildjuewkdxbcpsbjtsevkppvgilrlspejqvzpfeorjmrbdppovvpzxcytscycgwsbnmspihzldjdgilnrlmhaswqaqbecmaocesnpqaotamwofyyfsbmxidowusogmylhlhxftnrmhtnnljjhhcfvywsqimqxqobfsageysonuoagmmviozeouutsiecitrmkypwknorjjiaasxfhsftypspwhvqovmwkjuehujofiabznpipidhfxpoustquzyfurkcgmioxacleqdxgrxbldcuxzgbcazgfismcgmgtjuwchymkzoiqhzaqrtiykdkydgvuaqkllbsactntexcybbjaxlfhyvbxieelstduqzfkoceqzgncvexklahxjnvtyqcjtbfanzgpdmucjlqpiolklmjxnscjcyiybdkgitxnuvtmoypcdldrvalxcxalpwumfx").c_str());
  return 0;
}

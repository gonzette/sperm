#include <cstdio>
#include <cstdlib>
#include <string>
using namespace std;

class Solution {
public:
  int minCut(string s) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    const int size = s.size();

    int* pcache = new int[size * size];
    memset(pcache, 0xff, sizeof(int) * size * size);
    for(int i = 0; i < s.size(); i++) {
      for(int j = 0; j < s.size(); j++) {
        preCalculatePalindrome(s, i, j, pcache);
      }
    }

    int* cache = new int[size];
    memset(cache, 0xff, sizeof(int) * size);

    int code = minCutWithCache(s, 0, cache, pcache);

    delete[] pcache;
    delete[] cache;

    return code;
  }

  int minCutWithCache(const string& str, int offset, int cache[], int pcache[]) {
    if(offset >= (str.size() - 1)) {
      return 0;
    }
    if(cache[offset] != -1) {
      return cache[offset];
    }
    if(isPalindrome(str, offset, str.size() - 1, pcache)) {
      return 0;
    }
    int minValue = str.size();
    for(int i = offset + 1; i < str.size(); i++) {
      if(isPalindrome(str, offset, i - 1, pcache)) {
        int v = 1 + minCutWithCache(str, i, cache, pcache);
        if(v < minValue) {
          minValue = v;
        }
      }
    }
    cache[offset] = minValue;
    return minValue;
  }

  bool isPalindrome(const string& str, int s, int e, int pcache[]) {
    int index = s * str.size() + e;
    return pcache[index];
  }

  int preCalculatePalindrome(const string& str, int s, int e, int cache[]) {
    int index = s * str.size() + e;
    if(cache[index] != -1) {
      return cache[index] == 1;
    }
    int v = 1;
    if(s < e) {
      if(str[s] != str[e]) {
        v = 0;
      } else {
        v = preCalculatePalindrome(str, s + 1, e - 1, cache);
      }
    }
    cache[index] = v;
    return v;
  }
};

int main() {
  Solution s;
  printf("%d\n", s.minCut("aab"));
  printf("%d\n", s.minCut("aabbaa"));
  printf("%d\n", s.minCut("aabccb"));
  printf("%d\n", s.minCut("aabcc"));
  printf("%d\n", s.minCut("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
  return 0;
}

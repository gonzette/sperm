/* coding:utf-8
 * Copyright (C) dirlt
 */
#include <cstdlib>
#include <cctype>
#include <cstring>
#include <cstdio>
#include <cmath>
#include <ctime>
#include <iostream>
#include <sstream>
#include <vector>
#include <string>
#include <map>
#include <set>
#include <algorithm>
#include <queue>
#include <stack>
using namespace std;
typedef long long ll;

class FoxAndClassroom {
public:
  string ableTo(int n, int m) {
    int thres = n * m;
    for(int r = 0; r < n; r++) {
      for(int c = 0; c < m; c++) {
        int i = 1;
        int x = r;
        int y = c;
        for(; i < thres; i++) {
          x = (x + 1) % n;
          y = (y + 1) % m;
          if(x == r && y == c) break;
        }
        if(i == thres) return "Possible";
      }
    }
    return "Impossible";
  }

  // BEGIN CUT HERE
public:
  void run_test(int Case) {
    if ((Case == -1) || (Case == 0)) test_case_0();
    if ((Case == -1) || (Case == 1)) test_case_1();
    if ((Case == -1) || (Case == 2)) test_case_2();
    if ((Case == -1) || (Case == 3)) test_case_3();
    if ((Case == -1) || (Case == 4)) test_case_4();
    if ((Case == -1) || (Case == 5)) test_case_5();
  }
private:
  template <typename T> string print_array(const vector<T>& V) {
    ostringstream os;
    os << "{ ";
    for (typename vector<T>::const_iterator iter = V.begin(); iter != V.end(); ++iter) os << '\"' << *iter << "\",";
    os << " }";
    return os.str();
  }
  void verify_case(int Case, const string& Expected, const string& Received) {
    cerr << "Test Case #" << Case << "...";
    if (Expected == Received) cerr << "PASSED" << endl;
    else {
      cerr << "FAILED" << endl;
      cerr << "\tExpected: \"" << Expected << '\"' << endl;
      cerr << "\tReceived: \"" << Received << '\"' << endl;
    }
  }
  void test_case_0() {
    int Arg0 = 2;
    int Arg1 = 3;
    string Arg2 = "Possible";
    verify_case(0, Arg2, ableTo(Arg0, Arg1));
  }
  void test_case_1() {
    int Arg0 = 2;
    int Arg1 = 2;
    string Arg2 = "Impossible";
    verify_case(1, Arg2, ableTo(Arg0, Arg1));
  }
  void test_case_2() {
    int Arg0 = 4;
    int Arg1 = 6;
    string Arg2 = "Impossible";
    verify_case(2, Arg2, ableTo(Arg0, Arg1));
  }
  void test_case_3() {
    int Arg0 = 3;
    int Arg1 = 6;
    string Arg2 = "Impossible";
    verify_case(3, Arg2, ableTo(Arg0, Arg1));
  }
  void test_case_4() {
    int Arg0 = 5;
    int Arg1 = 7;
    string Arg2 = "Possible";
    verify_case(4, Arg2, ableTo(Arg0, Arg1));
  }
  void test_case_5() {
    int Arg0 = 10;
    int Arg1 = 10;
    string Arg2 = "Impossible";
    verify_case(5, Arg2, ableTo(Arg0, Arg1));
  }

  // END CUT HERE

};

// BEGIN CUT HERE
int main() {
  FoxAndClassroom ___test;
  ___test.run_test(-1);
  return 0;
}
// END CUT HERE

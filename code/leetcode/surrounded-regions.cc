#include <cstdio>
#include <cstring>
#include <vector>
#include <string>

using namespace std;

class Solution {
public:
  int N;
  int M;
  int* mark;
  typedef vector< vector<char> > VVC;
  void solve(VVC& board) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    N = board.size();
    if(N == 0) {
      return ;
    }
    M = board[0].size();
    if(M == 0) {
      return ;
    }
    mark = new int[N * M];
    memset(mark, 0, sizeof(int) * N * M);
    // O is not surrounded
    for(int i = 0; i < N; i++) {
      for(int j = 0; j < M; j++) {
        if(i == 0 || j == 0 || i == (N - 1) || j == (M - 1)) {
          if(board[i][j] == 'O') {
            solveWithMark(board, i, j, mark);
          }
        }
      }
    }
    flipWithMark(board, mark);
    delete[] mark;
  }

  int getIndex(int i, int j) {
    return i * N + j;
  }

  static const int UNVISITED = 0;
  static const int NOT_SUR = 1;

  void solveWithMark(VVC& board, int i, int j, int* mark) {
    if(i < 0 || i >= N) {
      return ;
    }
    if(j < 0 || j >= M) {
      return ;
    }
    int index = getIndex(i, j);
    if(mark[index] != UNVISITED || board[i][j] == 'X') {
      return ;
    }
    mark[index] = NOT_SUR;
    solveWithMark(board, i - 1, j, mark);
    solveWithMark(board, i + 1, j, mark);
    solveWithMark(board, i, j - 1, mark);
    solveWithMark(board, i, j + 1, mark);
  }

  void flipWithMark(VVC& board, int* mark) {
    for(int i = 0; i < N; i++) {
      for(int j = 0; j < M; j++) {
        int index = getIndex(i, j);
        if(board[i][j] == 'O' && mark[index] != NOT_SUR) {
          board[i][j] = 'X';
        }
      }
    }
  }

  void printVVC(const VVC& board) {
    for(int i = 0; i < N; i++) {
      for(int j = 0; j < M; j++) {
        printf("%c ", board[i][j]);
      }
      printf("\n");
    }
  }
};

int main() {
  Solution s;
  // {
  //   char b[4][4] = {
  //     'X','X','X','X',
  //     'X','O','O','X',
  //     'X','X','O','X',
  //     'X','O','X','X',
  //   };
  //   vector< vector<char> > board;
  //   for(int i=0;i<4;i++) {
  //     vector<char> vc;
  //     for(int j=0;j<4;j++) {
  //       vc.push_back(b[i][j]);
  //     }
  //     board.push_back(vc);
  //   }
  //   s.solve(board);
  //   s.printVVC(board);
  // }

  // {
  //   char b[2][2] = {
  //     'O','O',
  //     'O','O'
  //   };
  //   vector< vector<char> > board;
  //   for(int i=0;i<2;i++) {
  //     vector<char> vc;
  //     for(int j=0;j<2;j++) {
  //       vc.push_back(b[i][j]);
  //     }
  //     board.push_back(vc);
  //   }
  //   s.solve(board);
  //   s.printVVC(board);
  // }

  {
    const char* b[6] = {
      "OOOOXX",
      "OOOOOO",
      "OXOXOO",
      "OXOOXO",
      "OXOXOO",
      "OXOOOO"
    };
    vector< vector<char> > board;
    for(int i = 0; i < 6; i++) {
      vector<char> vc;
      for(int j = 0; j < 6; j++) {
        vc.push_back(b[i][j]);
      }
      board.push_back(vc);
    }
    s.solve(board);
    s.printVVC(board);
  }

  return 0;
}

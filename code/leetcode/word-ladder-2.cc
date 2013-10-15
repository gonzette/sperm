#include <string>
#include <set>
//#include <unordered_set>
#define unordered_set set
#include <cstring>
#include <vector>
#include <algorithm>
#include <queue>

using namespace std;

typedef vector< vector<string> > VVS;
typedef vector<string> VS;

class Solution {
public:
  int N;
  vector<string> res;
  char* dist;

  vector< vector<string> > findLadders(string start, string end, unordered_set<string>& dict) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    res.clear();
    res.push_back(start);
    for(unordered_set<string>::iterator it = dict.begin(); it != dict.end(); ++it) {
      res.push_back(*it);
    }
    res.push_back(end);
    N = res.size();
    //printf("N = %d\n", N);

    // construct dist
    dist = new char [N * N];
    memset(dist, 0, sizeof(char) * N * N);
    calcDist();
    printf("%d\n", getMinDist());

    calcFloyd();
    VVS vvs = getDescription(0);

    delete[] dist;
    return vvs;
  }

  int getIndex(int i, int j) {
    return i * N + j;
  }

  bool isAdjacent(int i, int j) {
    int diff = 0;
    for(int x = 0; x < res[i].size(); x++) {
      if(res[i][x] != res[j][x]) {
        diff++;
      }
    }
    return diff <= 1;
  }

  void printDist() {
    printf("--------------------\n");
    for(int i = 0; i < N; i++) {
      for(int j = 0; j < N; j++) {
        printf("%2d ", dist[getIndex(i, j)]);
      }
      printf("\n");
    }
  }

  void calcDist() {
    for(int i = 0; i < N; i++) {
      for(int j = i; j < N; j++) {
        if(i == j) {
          dist[getIndex(i, j)] = 0;
        } else if(isAdjacent(i, j)) {
          dist[getIndex(i, j)] = 1;
          dist[getIndex(j, i)] = 1;
        } else {
          dist[getIndex(i, j)] = -1;
          dist[getIndex(j, i)] = -1;
        }
      }
    }
  }


  // peformance-tuned.
  // but not good enough.
  void calcFloyd() {
    for(int k = 0; k < N; k++) {
      int ik = k;
      for(int i = 0; i < N; i++, ik += N) {
        if(dist[ik] == -1) {
          continue;
        }
        int kj = getIndex(k, i + 1);
        int ij = getIndex(i, i + 1);
        for(int j = i + 1; j < N; j++, kj++, ij++) {
          if(dist[kj] == -1) {
            continue;
          }
          int d = dist[ik] + dist[kj];
          if(dist[ij] == -1 || d < dist[ij]) {
            dist[ij] = d;
          }
          dist[getIndex(j, i)] = dist[ij];
        }
      }
    }
  }

  VVS getDescription(int f) {
    if(f == (N - 1)) {
      VVS vvs;
      VS vs;
      vs.push_back(res[f]);
      vvs.push_back(vs);
      return vvs;
    } else {
      VVS vvs;
      for(int i = 0; i < N; i++) {
        if(dist[getIndex(f, i)] == 1 &&
            dist[getIndex(i, N - 1)] == (dist[getIndex(f, N - 1)] - 1)) {
          //printf("f = %d, i = %d\n",f,i);
          VVS vvs2 = getDescription(i);
          for(int m = 0; m < vvs2.size(); m++) {
            VS vs;
            vs.push_back(res[f]);
            for(int x = 0; x < vvs2[m].size(); x++) {
              vs.push_back(vvs2[m][x]);
            }
            vvs.push_back(vs);
          }
        }
      }
      return vvs;
    }
  }

  // NOTE(dirlt):not think through.
  int getMinDist() {
    set<int> mask;
    queue<int> Q1;
    queue<int> Q2;
    Q1.push(0);
    mask.insert(0);
    Q2.push(0);
    while(!Q1.empty()) {
      int n = Q1.front();
      Q1.pop();
      int d = Q2.front();
      Q2.pop();
      if(n == (N - 1)) {
        return d;
      }
      for(int i = 0; i < N; i++) {
        if(mask.find(i) == mask.end() && dist[getIndex(n, i)] == 1) {
          mask.insert(i);
          Q1.push(i);
          Q2.push(d + 1);
        }
      }
    }
  }

  void printVVS(const VVS& vvs) {
    for(int i = 0; i < vvs.size(); i++) {
      printf("[");
      for(int j = 0; j < vvs[i].size(); j++) {
        printf("%s ", vvs[i][j].c_str());
      }
      printf("]\n");
    }
  }
};

int main() {
  Solution sol;
  {
    unordered_set<string> ss;
    const char* dict[] = {
      "hot", "dot", "dog", "lot", "log", NULL,
    };
    for(int i = 0; dict[i]; i++) {
      ss.insert(string(dict[i]));
    }

    VVS vvs = sol.findLadders("hit", "cog", ss);
    sol.printVVS(vvs);
  }
  {
    unordered_set<string> ss;
    const char* dict[] = { "kid", "tag", "pup", "ail", "tun", "woo", "erg", "luz", "brr", "gay", "sip", "kay", "per", "val", "mes", "ohs", "now", "boa", "cet", "pal", "bar", "die", "war", "hay", "eco", "pub", "lob", "rue", "fry", "lit", "rex", "jan", "cot", "bid", "ali", "pay", "col", "gum", "ger", "row", "won", "dan", "rum", "fad", "tut", "sag", "yip", "sui", "ark", "has", "zip", "fez", "own", "ump", "dis", "ads", "max", "jaw", "out", "btu", "ana", "gap", "cry", "led", "abe", "box", "ore", "pig", "fie", "toy", "fat", "cal", "lie", "noh", "sew", "ono", "tam", "flu", "mgm", "ply", "awe", "pry", "tit", "tie", "yet", "too", "tax", "jim", "san", "pan", "map", "ski", "ova", "wed", "non", "wac", "nut", "why", "bye", "lye", "oct", "old", "fin", "feb", "chi", "sap", "owl", "log", "tod", "dot", "bow", "fob", "for", "joe", "ivy", "fan", "age", "fax", "hip", "jib", "mel", "hus", "sob", "ifs", "tab", "ara", "dab", "jag", "jar", "arm", "lot", "tom", "sax", "tex", "yum", "pei", "wen", "wry", "ire", "irk", "far", "mew", "wit", "doe", "gas", "rte", "ian", "pot", "ask", "wag", "hag", "amy", "nag", "ron", "soy", "gin", "don", "tug", "fay", "vic", "boo", "nam", "ave", "buy", "sop", "but", "orb", "fen", "paw", "his", "sub", "bob", "yea", "oft", "inn", "rod", "yam", "pew", "web", "hod", "hun", "gyp", "wei", "wis", "rob", "gad", "pie", "mon", "dog", "bib", "rub", "ere", "dig", "era", "cat", "fox", "bee", "mod", "day", "apr", "vie", "nev", "jam", "pam", "new", "aye", "ani", "and", "ibm", "yap", "can", "pyx", "tar", "kin", "fog", "hum", "pip", "cup", "dye", "lyx", "jog", "nun", "par", "wan", "fey", "bus", "oak", "bad", "ats", "set", "qom", "vat", "eat", "pus", "rev", "axe", "ion", "six", "ila", "lao", "mom", "mas", "pro", "few", "opt", "poe", "art", "ash", "oar", "cap", "lop", "may", "shy", "rid", "bat", "sum", "rim", "fee", "bmw", "sky", "maj", "hue", "thy", "ava", "rap", "den", "fla", "auk", "cox", "ibo", "hey", "saw", "vim", "sec", "ltd", "you", "its", "tat", "dew", "eva", "tog", "ram", "let", "see", "zit", "maw", "nix", "ate", "gig", "rep", "owe", "ind", "hog", "eve", "sam", "zoo", "any", "dow", "cod", "bed", "vet", "ham", "sis", "hex", "via", "fir", "nod", "mao", "aug", "mum", "hoe", "bah", "hal", "keg", "hew", "zed", "tow", "gog", "ass", "dem", "who", "bet", "gos", "son", "ear", "spy", "kit", "boy", "due", "sen", "oaf", "mix", "hep", "fur", "ada", "bin", "nil", "mia", "ewe", "hit", "fix", "sad", "rib", "eye", "hop", "haw", "wax", "mid", "tad", "ken", "wad", "rye", "pap", "bog", "gut", "ito", "woe", "our", "ado", "sin", "mad", "ray", "hon", "roy", "dip", "hen", "iva", "lug", "asp", "hui", "yak", "bay", "poi", "yep", "bun", "try", "lad", "elm", "nat", "wyo", "gym", "dug", "toe", "dee", "wig", "sly", "rip", "geo", "cog", "pas", "zen", "odd", "nan", "lay", "pod", "fit", "hem", "joy", "bum", "rio", "yon", "dec", "leg", "put", "sue", "dim", "pet", "yaw", "nub", "bit", "bur", "sid", "sun", "oil", "red", "doc", "moe", "caw", "eel", "dix", "cub", "end", "gem", "off", "yew", "hug", "pop", "tub", "sgt", "lid", "pun", "ton", "sol", "din", "yup", "jab", "pea", "bug", "gag", "mil", "jig", "hub", "low", "did", "tin", "get", "gte", "sox", "lei", "mig", "fig", "lon", "use", "ban", "flo", "nov", "jut", "bag", "mir", "sty", "lap", "two", "ins", "con", "ant", "net", "tux", "ode", "stu", "mug", "cad", "nap", "gun", "fop", "tot", "sow", "sal", "sic", "ted", "wot", "del", "imp", "cob", "way", "ann", "tan", "mci", "job", "wet", "ism", "err", "him", "all", "pad", "hah", "hie", "aim", "ike", "jed", "ego", "mac", "baa", "min", "com", "ill", "was", "cab", "ago", "ina", "big", "ilk", "gal", "tap", "duh", "ola", "ran", "lab", "top", "gob", "hot", "ora", "tia", "kip", "han", "met", "hut", "she", "sac", "fed", "goo", "tee", "ell", "not", "act", "gil", "rut", "ala", "ape", "rig", "cid", "god", "duo", "lin", "aid", "gel", "awl", "lag", "elf", "liz", "ref", "aha", "fib", "oho", "tho", "her", "nor", "ace", "adz", "fun", "ned", "coo", "win", "tao", "coy", "van", "man", "pit", "guy", "foe", "hid", "mai", "sup", "jay", "hob", "mow", "jot", "are", "pol", "arc", "lax", "aft", "alb", "len", "air", "pug", "pox", "vow", "got", "meg", "zoe", "amp", "ale", "bud", "gee", "pin", "dun", "pat", "ten", "mob", NULL};
    for(int i = 0; dict[i]; i++) {
      ss.insert(string(dict[i]));
    }

    VVS vvs = sol.findLadders("cet", "ism", ss);
    sol.printVVS(vvs);
  }
  return 0;
}

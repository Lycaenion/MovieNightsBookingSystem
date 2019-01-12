class Movie {

    get imdbid(){
        return this._imdbid;
    }
    get title(){
        return this._title;
    }
    get year(){
        return this._year;
    }
    get rated(){
        return this._rated;
    }
    get runtime(){
        return this._runtime;
    }
    get genre(){
        return this._genre;
    }
    get plot(){
        return this._plot;
    }
    get langauge(){
        return this._language;
    }
    get poster(){
        return this._poster;
    }
    set imdbid(imdbid){
        this._imdbid = imdbid;
    }

    set year(year){
        this._year = year;
    }

    set title(title){
        this._title = title;
    }
}
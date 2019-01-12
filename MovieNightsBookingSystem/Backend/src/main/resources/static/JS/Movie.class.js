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
    set title(title){
        this._title = title;
    }
    set year(year){
        this._year = year;
    }

    set rated(rated){
        this._rated = rated;
    }

    set runtime(runtime){
        this._runtime = runtime;
    }

    set genre(genre){
        this._genre = genre;
    }

    set plot(plot){
        this._plot = plot;
    }

    set language(language){
        this._language = language;
    }

    set poster(poster){
        this._poster = poster;
    }
}
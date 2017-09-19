module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    clean: {
      dist: {
        src: ['dist']
      }
    },
    ngAnnotate: {
      options: {
        singleQuotes: true
      },
      dist: {
        expand: true,
        cwd: 'src',
        src: ['app/**/*.js'],
        dest: 'dist'
      }
    },
    concat: {
      js: {
        options: {
          separator: ';'
        },
        src: ['src/lib/jquery.js','src/lib/jquery.ui.js','src/lib/underscore.js','src/lib/utils.js', 'src/lib/angular.js', 'src/lib/angular.*.js',
          'dist/app/app.js', 'dist/app/controller/*.js', 'dist/app/directive/*.js', 'dist/app/filter/*.js', 'dist/app/service/*.js'],
        dest: 'dist/music.js'
      },
      css: {
        src: ['src/style/*.css', 'dist/less.css'],
        dest: 'dist/music.css'
      }
    },
    less: {
      dist: {
        src: ['src/style/*.less'],
        dest: 'dist/less.css'
      }
    },
    cssmin: {
      dist: {
        src: 'dist/music.css',
        dest: 'dist/style/music.min.css'
      }
    },
    uglify: {
      dist: {
        src: 'dist/music.js',
        dest: 'dist/music.min.js'
      }
    },
    copy: {
      dist: {
        expand: true,
        cwd: 'src/',
        src: ['**', '!**/*.js', '!index.html', '!**/*.less', '!**/*.css'],
        dest: 'dist/'
      }
    },
    htmlrefs: {
      dist: {
        src: 'src/index.html',
        dest: 'dist/index.html'
      }
    },
    remove: {
      dist: {
        fileList: ['dist/music.css', 'dist/music.js', 'dist/less.css'],
        dirList: ['dist/app']
      }
    },
    cleanempty: {
      options: {
        files: false,
        folders: true
      },
      src: ['dist/**']
    },
    replace: {
      dist: {
        src: ['dist/music.min.js', 'dist/**/*.html'],
        overwrite: true,
        replacements: [{
          from: '../api',
          to: grunt.option('apiurl') || '../api'
        },{
          from: '../ws',
          to: grunt.option('wsurl') || '../ws'
        }]
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-cleanempty');
  grunt.loadNpmTasks('grunt-htmlrefs');
  grunt.loadNpmTasks('grunt-css');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-remove');
  grunt.loadNpmTasks('grunt-ng-annotate');
  grunt.loadNpmTasks('grunt-text-replace');

  // Default tasks.
  grunt.registerTask('default', ['clean', 'ngAnnotate', 'concat:js', 'less', 'concat:css', 'cssmin',
    'uglify', 'copy', 'remove', 'cleanempty', 'htmlrefs', 'replace']);

};
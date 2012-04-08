require 'rubygems'
require 'rake/clean'

$build_count = 0
$target_directory = "target/scala-2.9.1/resource_managed/main/coffee"
CLEAN.include($target_directory)

#======================================================================
# Check for the existence of required executables.
#======================================================================
def check(exec, name, url)
  return unless `which #{exec}`.empty?
  puts "#{name} not found.\nInstall it with #{url}"
  exit
end
check 'coffeejade', 'CoffeeScript Jade', 'git clone git://github.com/fusesource/coffeejade.git && sudo npm install -g coffeejade'
check 'coffee', 'CoffeeScript', 'sudo npm install -g coffee-script'


#======================================================================
# Handle copying the src/main/webapp files to target/webapp
#======================================================================
desc "Copies the webapp files accross."
task :webapp do
end

def find_webapp_files
  FileList['src/main/webapp/admin/app/**/*'].each do |src|
    target = src.sub(/^src\/main\/webapp\//, "#{$target_directory}/")
    if !Rake::Task.task_defined?(target)
      file target => [src]  do |t|
        dir = File.dirname(target)
        mkdir_p dir if !File.exist?(dir)
        cp_r src, target, :verbose=>false
      end
    end
    task :webapp => [target]
  end
end
find_webapp_files

#======================================================================
# Handle the .coffee files.
#======================================================================
rule '.js' => '.coffee' do |file|
  $build_count += 1
  puts "coffee -c -b #{file.source}"
  result = `coffee -c -b #{file.source} 2>&1`
  if $?!=0
    touch file.name
    raise result
  end
end

desc "Builds all the coffee script files"
task :coffee => [:webapp] do
end

def find_coffee_files
  FileList["src/main/webapp/admin/app/**/*.coffee"].each do |coffee|
    coffee = coffee.sub(/^src\/main\/webapp\//, "#{$target_directory}/")
    target = coffee.sub(/\.coffee$/, '.js')
    if !Rake::Task.task_defined?(target)
      file target => [coffee]
      task :coffee => [target]
    end
  end
end
find_coffee_files

#======================================================================
# Handle the .jade files.
#======================================================================
JADE_JS = "#{$target_directory}/admin/app/views/jade.js"
file JADE_JS do
  $build_count += 1
  puts "coffeejade --amdout jade.js --amdrequire 'admin-app/frameworks'"
  result = `cd #{$target_directory}/admin/app/views && coffeejade --amdout jade.js --amdrequire 'frameworks' * 2>&1`
  if $?!=0
    touch JADE_JS
    raise result
  end
end

desc "Builds all the jade templates"
task :jade => [JADE_JS] do
end

def find_jade_files
  FileList["src/main/webapp/admin/app/views/**/*.jade"].each do |jade|
    jade = jade.sub(/^src\/main\/webapp\//, "#{$target_directory}/")
    file JADE_JS => [jade]
  end
end
find_jade_files


#======================================================================
# Setup the Tasks.
#======================================================================
task :default => ["coffee", "jade"]

desc "Watch for changes to recompile"
task :watch do
  require 'date'
  
  while true
    find_webapp_files
    find_coffee_files
    find_jade_files
    Rake::Task.tasks.each do |task|
      task.reenable
    end  
    begin
      $build_count = 0
      Rake::Task[:default].invoke
      # puts Rake::Task[JADE_JS].prerequisites
      if $build_count > 0 
        puts "Application built at: "+DateTime.now.strftime("%I:%M:%S %p")+", now waiting for file changes"
      end
    rescue
      `/usr/local/bin/growlnotify -t 'Compile failed' -m '#{$!}'`
      puts "Compile failure:"
      puts "=======================================================================\n"
      puts "#{$!}"
      puts "=======================================================================\n"
      puts "Compile failed at: "+DateTime.now.strftime("%I:%M:%S %p")+", now waiting for file changes"
    end
    sleep 1
  end
end

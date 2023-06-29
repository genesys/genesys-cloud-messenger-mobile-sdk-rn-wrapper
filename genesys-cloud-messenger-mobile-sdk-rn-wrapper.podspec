require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = 'genesys-cloud-messenger-mobile-sdk-rn-wrapper'
  s.version      = package["version"]
  s.summary      = 'Genesys Cloud Messenger Mobile SDK wrapper for React Native.'
  s.homepage     = 'https://genesys.com'
  s.license      = { :type => 'MIT' }
  s.authors      = 'Genesys'
  s.platforms    = { :ios => '13.0' }
  s.source       = { :git => 'https://github.com/genesys/genesys-cloud-messenger-mobile-sdk-rn-wrapper.git', :tag => '#{s.version}' }
  s.source_files = "ios/**/*.{h,m,mm}"
  s.dependency 'React-Core'
  s.dependency 'GenesysCloud', '4.1.0'
  s.static_framework = true
end

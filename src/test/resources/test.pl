#!/usr/bin/perl

sub test_handle {
  $arg = shift;
  return "test_data_response $arg ";
}

sub test_handle_timeout {
  sleep;
}
1;
